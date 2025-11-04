<p align ="center">
    <img src="https://github.com/UTSA-CS-3443-Fall25/7.IdeaForgers/blob/main/BiteWise/src/main/resources/edu/utsa/cs3443/group7/bitewise/ui_icons/logo.png" alt="logo.png" />
</p>

# 🍏 BiteWise: Food Detection App 


A JavaFX desktop application that uses a YOLO ONNX model to detect and identify food items from an image. This project was built for the CS 3443 (Applicaion Programming) course at UTSA.

## Features

* Load local image files (`.jpg`, `.png`, etc.).
* Analyzes images using an AI model.
* Detects 211 different food classes.
* Draws bounding boxes and confidence scores directly on the image.

---
## 📋 Prerequisites

Before you begin, ensure you have the following installed on your system:

* Java Development Kit (JDK) 21 or higher
* Apache Maven

---
## 🚀 How to Install and Run

1.  **Clone the repository into your IDE:**
    ```
    git clone https://github.com/UTSA-CS-3443-Fall25/7.IdeaForgers.git
    ```

2.  **Run the application using Maven:**
    
    # Setup (⚠IMPORTANT for IntelliJ or other IDE⚠ )

    This is a Maven project. To run it, you **must** import it as a Maven project in IntelliJ so it can download the JavaFX dependencies.

    ## How to import dependencies 

    1. Open IntelliJ and clone the repository from GitHub
    2. Right-click on the `pom.xml` file in your project sidebar.
    3.  Find and click on **"Add as Maven Project"**.
    4.  Wait for the dependencies to download.
  
    Then you can run the program

---

## Project code structure ( so far)
```
├───src
│   └───main
│       ├───java
│       │   │   module-info.java
│       │   │   
│       │   ├───controller
│       │   │       InterferenceController.java
│       │   │       
│       │   ├───detectionApp
│       │   │       AppLauncher.java
│       │   │       Main.java
│       │   │       
│       │   ├───model
│       │   │       Detection.java
│       │   │       
│       │   └───utility
│       │   |       DetectionDrawer.java
|       |   |       ModelLoader.java
│       │           
│       └───resources
│           └───edu
│               └───utsa
│                   └───cs3443
│                       └───group7
│                           └───bitewise
│                               │   best.onnx
│                               │   scan-screen.fxml
│                               │   sign-in.fxml
│                               │   style.css
│                               │   
│                               └───ui_icons
│                                     │  logo.png
│                                     │   ....
```
---

## ScreenShot

<img src="https://github.com/UTSA-CS-3443-Fall25/7.IdeaForgers/blob/main/BiteWise/src/main/resources/edu/utsa/cs3443/group7/bitewise/images/test1.png" />
## 👨‍💻 Authors

* Phu Pham
* Angel Nguyen
* (Teammate name here )
* ...
