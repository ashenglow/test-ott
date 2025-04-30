import json
import os

base_dir = os.path.dirname(os.path.abspath(__file__))
json_path = os.path.join(base_dir, 'originalVideo.json')
with open(json_path, 'r', encoding='utf-8') as f:
    data = json.load(f)


def lower_keys(d):
    return {k.lower(): v for k, v in d.items()}

lowercased_data = [lower_keys(video) for video in data]

with open('video_lowercased.json', 'w',encoding='utf-8') as f:
    json.dump(lowercased_data, f, ensure_ascii=False, indent=2)
