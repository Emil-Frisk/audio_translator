# Translate Audio
With this app you can upload English audio files and select a target language, and it will transform that audio into a translated transcript in your selected target language.
![Hope Page](imgs/homepage.jpg)


## How to use it
![Dashboard Page](imgs/homepage.jpg)

# How to run it
Make sure you have Docker installed, if you are on Windows you have to open Docker Desktop on the background. After that just run docker-compose up in the folder where docker-compose.yml resides. This will take a while, because the image is quite large. When its ready it will spin up 2 containers, the app and the database that the app uses. Now you can go to 127.0.0.1:8080/ and the website should be running.

### python version
3.11.9

