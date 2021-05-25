from tensorflow.keras.models import load_model
import numpy as np

import cv2
import os
import sys

# import chess

def to_FEN_label(label_id):
    mapper = {
        0: "b",
        1: "k",
        2: "n",
        3: "p",
        4: "q",
        5: "r",
        6: "1",
        7: "B",
        8: "K",
        9: "N",
        10: "P",
        11: "Q",
        12: "R"
    }
    return mapper[label_id]

def get_model():
  return load_model("model/model.h5", compile=False)

model = get_model()


# array of 64 images of size 160 x 160 x 3(RGB)
# each image is a tile of the board
def to_FEN(images, facing='white'):
    images = np.array(images)
    classes = np.argmax(model.predict(images), axis=-1)
    classes = list(map(to_FEN_label, classes))
    classes_by_line = [classes[i:i + 8] for i in range(0, len(classes), 8)]

    fen = ""

    if facing == 'black':
        classes_by_line.reverse()
    for line in classes_by_line:
        emptys = 0
        fen_line = ""
        if facing == 'black':
            line.reverse()
        for tile in line:
            if tile.isalpha():
                if emptys > 0:
                    fen_line += str(emptys)
                    emptys = 0
                fen_line += tile
            else:
                emptys += 1

        if emptys > 0:
            fen_line += str(emptys)
        fen_line += '/'
        fen += fen_line

    return fen[:-1] + ' w KQkq - 0 1'


def load_images_from_folder(folder):
    images = []
    for i in range(64):
        img = cv2.imread(os.path.join(folder, str(i + 1) + ".jpg"))
        if img is not None:
            images.append(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
    return images


if __name__ == "__main__":
    images = load_images_from_folder("Images/squares")
    print(to_FEN(images))


