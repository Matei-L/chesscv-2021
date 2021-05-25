from flask import Flask
from flask import request
import os
import separate_in_squares
import FENDetection

app = Flask(__name__)

@app.route('/')
def hello_worl():
    return 'Hello,World!'


@app.route('/board', methods=['POST'])
def create_board_from_image():
    request.get_json()
    if "img" not in request.files:
        return "No image found", 400

    img = request.files['img']
    img.save("Images/receivedImg.jpg")
    os.system('python neural-chessboard/main.py detect --input=Images/receivedImg.jpg --output=Images/board.jpg')

    response = dict()
    response["url"] = "http://192.168.0.148:8000/board.jpg"
    return response


@app.route('/fen', methods=['POST'])
def create_fen_from_board():
    data = request.get_json()
    # url = data.get('url', '')
    facing = data.get('facing', 'white')
    separate_in_squares.separateIn64Squares("Images/board.jpg")

    images = FENDetection.load_images_from_folder("Images/squares")

    response = dict()
    response["fen"] = FENDetection.to_FEN(images, facing)
    return response

