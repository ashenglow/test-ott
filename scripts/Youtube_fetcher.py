import json

from googleapiclient.discovery import build
import os

base_dir = os.path.dirname(os.path.abspath(__file__))
json_path = os.path.join(base_dir, 'video_lowercased.json')

API_KEY = "AIzaSyC3-RKB9Ku-KzTUqWOYIbH5dGjK9X65PvM"
youtube = build("youtube", "v3", developerKey=API_KEY)

def batch_crawl_movies():
    with open(json_path, 'r', encoding='utf-8') as f:
        movies = json.load(f)
    updated_movies = []

    for movie in movies:
        # 쿼리 생성: 제목 + 장르 (중복 제거)
        query = f"{movie['title']} {' '.join(set(movie['genre'].split()))}"

        # API 호출
        response = youtube.search().list(
            q=query,
            part="snippet",
            type="video",
            maxResults=1,
            order="relevance"
        ).execute()

        # 임베드 URL 생성
        if response.get('items'):
            item = response['items'][0]
            video_id = item['id']['videoId']

            # 2. 기존 movie 객체에 videoId만 추가
            updated_movie = {
                "id": str(movie['id']),  # <- id를 문자열로
                "title": movie['title'],
                "videoId": video_id,
                "thumbnail": item['snippet']['thumbnails']['high']['url'],
                "description": movie['synopsis'],  # <- synopsis를 description으로
            }
            updated_movies.append(updated_movie)

    # 결과 저장
    updated_path = os.path.join(base_dir, 'updated_movies.json')
    with open(updated_path, 'w', encoding='utf-8') as f:
        json.dump(updated_movies, f, ensure_ascii=False, indent=2)

# 실행
batch_crawl_movies()