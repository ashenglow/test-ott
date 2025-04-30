import json

from googleapiclient.discovery import build
import os

base_dir = os.path.dirname(os.path.abspath(__file__))
json_path = os.path.join(base_dir, 'video_lowercased.json')
updated_path = os.path.join(base_dir, 'updated_movies.json')

API_KEY = "AIzaSyC3-RKB9Ku-KzTUqWOYIbH5dGjK9X65PvM"
youtube = build("youtube", "v3", developerKey=API_KEY)

def parse_duration(duration_str):
    """ISO 8601 duration 문자열을 초 단위로 변환"""
    seconds = 0
    duration_str = duration_str.replace('PT', '')

    if 'H' in duration_str:
        hours, duration_str = duration_str.split('H')
        seconds += int(hours) * 3600

    if 'M' in duration_str:
        minutes, duration_str = duration_str.split('M')
        seconds += int(minutes) * 60

    if 'S' in duration_str:
        seconds += int(duration_str.replace('S', ''))

    return seconds

# 우선 키워드 (영상 title 기준)
priority_keywords = ['official', 'trailer','공식 예고', '공식 티저', '공식 예고편', '예고편', 'teaser', '티져', '예고']

def batch_crawl_movies():
    with open(json_path, 'r', encoding='utf-8') as f:
        movies = json.load(f)

    updated_movies = []

    for movie in movies:
        query = movie['title'] + ' 영화'

        # 1. 검색
        search_response = youtube.search().list(
            q=query,
            part="snippet",
            type="video",
            maxResults=5,
            order="relevance"
        ).execute()

        video_items = search_response.get('items', [])
        video_ids = [item['id']['videoId'] for item in video_items]

        if not video_ids:
            continue  # 검색 결과 없으면 패스

        # 2. 상세 조회로 duration 확인
        videos_response = youtube.videos().list(
            id=",".join(video_ids),
            part="contentDetails,snippet"
        ).execute()

        selected_video = None


        # 3. 우선순위 키워드 포함 + 쇼츠 제외
        for video in videos_response.get('items', []):
            title_lower = video['snippet']['title'].lower()
            duration_sec = parse_duration(video['contentDetails']['duration'])

            if duration_sec > 60 and any(kw in title_lower for kw in priority_keywords):
                selected_video = video
                break  # 우선순위 조건 만족하는 첫 번째 영상 선택

        # 4. 없으면 그나마 쇼츠 아닌 첫 번째 영상 선택
        if not selected_video:
            for video in videos_response.get('items', []):
                duration_sec = parse_duration(video['contentDetails']['duration'])
                if duration_sec > 60:
                    selected_video = video
                    break

        if not selected_video:
            continue  # 5개 다 쇼츠인 경우 skip

        # 5. movie 정보 + YouTube 영상 정보 결합
        video_id = selected_video['id']
        snippet = selected_video['snippet']
        updated_movie = {
                "id": str(movie['id']),
                "title": movie['title'],
                "videoId": video_id,
                "thumbnail": snippet['thumbnails']['high']['url'],
                "description": movie['synopsis'],  # 기존 synopsis를 description으로
            }
        updated_movies.append(updated_movie)


    # 6. 결과 저장
    with open(updated_path, 'w', encoding='utf-8') as f:
        json.dump(updated_movies, f, ensure_ascii=False, indent=2)

    print(f"batch_crawl_movies completed. Total videos collected: {len(updated_movies)}")

# 실행
batch_crawl_movies()
print("batch_crawl_movies completed.")