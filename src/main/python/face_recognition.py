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
            print(f"Loaded {len(known_face_encodings)} faces from database", file=sys.stderr)
            print(f"Known face IDs: {known_face_ids}", file=sys.stderr)
    else:
        print("Database file not found", file=sys.stderr)
    
    return known_face_encodings, known_face_ids

def recognize_face(image_path):
    print(f"Processing image: {image_path}", file=sys.stderr)
    
    # Load the image
    image = cv2.imread(image_path)
    if image is None:
        print("-1")  # Error: Could not read image
        return
    
    # Convert to grayscale
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    
    # Load the face cascade classifier
    face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
    
    # Detect faces
    faces = face_cascade.detectMultiScale(gray, 1.3, 5)
    print(f"Detected {len(faces)} faces in image", file=sys.stderr)
    
    if len(faces) == 0:
        print("-1")  # No face detected
        return
    
    # Load known faces from database
    known_face_encodings, known_face_ids = load_known_faces()
    
    if len(known_face_encodings) == 0:
        print("-2")  # No faces in database
        return
    
    # Get face encoding for the detected face
    face_encoding = cv2.face.LBPHFaceRecognizer_create()
    face_encoding.train([gray[y:y+h, x:x+w] for (x,y,w,h) in faces], np.array([0]))
    
    # Compare with known faces
    for i, known_encoding in enumerate(known_face_encodings):
        # Calculate similarity score
        score = face_encoding.predict(gray[y:y+h, x:x+w])
        print(f"Comparing with face {i}, score: {score}", file=sys.stderr)
        
        # If similarity is high enough, return the corresponding user ID
        if score[1] < 50:  # Lower score means better match
            print(f"Match found! User ID: {known_face_ids[i]}", file=sys.stderr)
            print(known_face_ids[i])  # Print the matched user ID
            return
    
    print("No match found (all scores above threshold)", file=sys.stderr)
    print("-1")  # No match found
    
if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python face_recognition.py <image_path>")
        sys.exit(1)
    
    image_path = sys.argv[1]
    recognize_face(image_path) 