from cv2 import cv2


def separateIn64Squares(path):
    img = cv2.imread(path)
    squares = []
    for line in range(0, 8):
        for column in range(0, 8):
            squares.append(img[line * 150: line * 150 + 150, column * 150: column * 150 + 150])

    i = 1
    for square in squares:
        cv2.imwrite("Images/squares/" + str(i) + ".jpg", square)
        i += 1


if __name__ == "__main__":
    separateIn64Squares("Images/board.jpg")