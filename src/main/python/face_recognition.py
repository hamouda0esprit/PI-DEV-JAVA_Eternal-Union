import cv2
import os
import sys
import numpy as np
import pickle
from pathlib import Path

def load_known_faces(database_path="face_database"):
    known_face_encodings = []
    known_face_ids = []
    
    # Load the face database if it exists
    if os.path.exists("face_database.pkl"):
        with open("face_database.pkl", "rb") as f:
            known_face_encodings, known_face_ids = pickle.load(f)
    
    return known_face_encodings, known_face_ids

def recognize_face(image_path):
    # Load the image
    image = cv2.imread(image_path)
    if image is None:
        print("Error: Could not read image")
        return
    
    # Convert to grayscale
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    
    # Load the face cascade classifier
    face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
    
    # Detect faces
    faces = face_cascade.detectMultiScale(gray, 1.3, 5)
    
    if len(faces) == 0:
        print("No match found")
        return
    
    # For now, just return a dummy user ID
    # In a real application, you would compare with known faces
    print("1")  # Return user ID 1 for testing
    
if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python face_recognition.py <image_path>")
        sys.exit(1)
    
    image_path = sys.argv[1]
    recognize_face(image_path) 