from flask import Flask, request, jsonify
import re
import sys
import os
import Mykytea

# 假设原始脚本在同一目录
sys.path.append(os.path.dirname(os.path.abspath(__file__)))
# 如果原始文件名为mm_address_ws.py，导入其中的preprocess函数
# from mm_address_ws import preprocess

app = Flask(__name__)
# 配置JSON响应不转义Unicode字符
app.config['JSON_AS_ASCII'] = False

# 直接复制原始代码中的preprocess函数
def preprocess(mm_text):
    mm_text = re.sub('့်', '့်', mm_text)
    mm_text = re.sub('ုိ', 'ို', mm_text)
    mm_text = re.sub('ံု', 'ုံ', mm_text)
    mm_text = re.sub('ဉီ', 'ဦ', mm_text)
    mm_text = re.sub('ျျ', 'ျ', mm_text)
    mm_text = re.sub('ြြ', 'ြ', mm_text)
    mm_text = re.sub('ွွ', 'ွ', mm_text)
    mm_text = re.sub('ှှ', 'ှ', mm_text)
    mm_text = re.sub('ိိ', 'ိ', mm_text)
    mm_text = re.sub('ီီ', 'ီ', mm_text)
    mm_text = re.sub('ုု', 'ု', mm_text)
    mm_text = re.sub('ူူ', 'ူ', mm_text)
    mm_text = re.sub('််', '်', mm_text)
    mm_text = re.sub('ာာ', 'ာ', mm_text)
    mm_text = re.sub('းး', 'း', mm_text)
    mm_text = re.sub('့့', '့', mm_text)
    mm_text = re.sub('ံံ', 'ံ', mm_text)
    mm_text = re.sub('ေေ', 'ေ', mm_text)
    return mm_text

# 初始化KyTea，使用与原代码相同的模型路径
opt = "-model /home/elastic/Address-WS/Address-Word-Segment-Model-5g-3w.dat"
mk = Mykytea.Mykytea(opt)

@app.route('/segment', methods=['POST'])
def segment():
    if 'text' in request.json:
        clean_input = preprocess(request.json['text'])
        ans = " ".join(mk.getWS(clean_input))
        output = re.sub('\s+', ' ', ans)
        return jsonify({"segmented_text": output})
    elif 'file' in request.files:
        file = request.files['file']
        content = file.read().decode('utf-8')
        clean_input = preprocess(content)
        ans = " ".join(mk.getWS(clean_input))
        ans = re.sub('\n +', '\n', ans)
        output = re.sub(' +', ' ', ans)
        return jsonify({"segmented_text": output})
    else:
        return jsonify({"error": "请提供'text'参数或上传文件"}), 400

if __name__ == "__main__":
    app.run(debug=True, host='0.0.0.0', port=5000) 