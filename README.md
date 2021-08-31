# MapGo

![PNU Symbol](https://www.pusan.ac.kr/_contents/kor/_Img/07Intro/ui07.jpg)

[![License](https://img.shields.io/github/license/PNU-Sinbaram/MapGo)](./LICENSE)
[![Issues](https://img.shields.io/github/issues/PNU-Sinbaram/MapGo)](https://github.com/PNU-Sinbaram/MapGo/issues)
[![Stars](https://img.shields.io/github/stars/PNU-Sinbaram/MapGo)](https://github.com/PNU-Sinbaram/MapGo)
[![Forks](https://img.shields.io/github/forks/PNU-Sinbaram/MapGo)](https://github.com/PNU-Sinbaram/MapGo)
[![Client github action](https://github.com/PNU-Sinbaram/MapGo/actions/workflows/client-ci.yml/badge.svg?branch=main)](https://github.com/PNU-Sinbaram/MapGo/actions)
[![Server github action](https://github.com/PNU-Sinbaram/MapGo/actions/workflows/server-ci.yml/badge.svg?branch=main)](https://github.com/PNU-Sinbaram/MapGo/actions)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/a7f32fbc7a6e4048890859549677d53f)](https://www.codacy.com/gh/PNU-Sinbaram/MapGo/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=PNU-Sinbaram/MapGo&amp;utm_campaign=Badge_Grade)
[![Hackathon badge](https://img.shields.io/badge/WIP-SW%20Hackathon%202021-blueviolet)](https://github.com/PNU-Sinbaram/MapGo)

## Client Build Guide

Before build client app, you need to create `apikey.properties` file in client
directory and fill it with your own secret key pairs.

Here is quick commands for key file generation
```bash
echo -e "SERVER_ADDRESS=${SERVER_ADDRESS}\nNAVER_KEY_ID=${NAVER_API_ID}\nNAVER_SECRET_KEY=${NAVER_API_SECRET}\nNAVER_OPEN_KEY_ID=${NAVER_OPEN_KEY_ID}\nNAVER_OPEN_SECRET_KEY=${NAVER_OPEN_SECRET_KEY}" >> ./client/apikey.properties
```

After generate key file, you can build MapGo application using below commands
```bash
cd client
bash ./gradlew assembleDebug --stacktrace
```

## Server Build Guide

You have two options for distributing mapgo server to your environment
### Docker
```bash
docker pull snowapril/mapgo_server
docker run -e DJANGO_SECRET_KEY=RANDOM_SECRET_KEY -p 8000:8000 snowapril/mapgo_server
```

### On your local
```bash
cd server
pip install --upgrade pip==21.2.4
pip install -r requirements.txt
python3 manage.py makemigrations && python3 manage.py migrate && 	python3 manage.py runserver 0:8000
```

## Documents
[![image](https://user-images.githubusercontent.com/24654975/131485708-a38a6988-e4f6-4287-ac80-472c594d75da.png)](https://snowapril.notion.site/SW-eeb1ec6eb0194c40b3b8c2a9445933bd)

https://snowapril.notion.site/SW-eeb1ec6eb0194c40b3b8c2a9445933bd

## Figma
![image](https://user-images.githubusercontent.com/24654975/124858409-f45bdd80-dfe8-11eb-9805-fd11302c2b8e.png)

## License
<img align="right" src="http://opensource.org/trademarks/opensource/OSI-Approved-License-100x137.png">

The class is licensed under the [MIT License](http://opensource.org/licenses/MIT):

Copyright (c) 2021 Sinbaram Team
*   [Jihong Shin](https://github.com/Snowapril)
*   [Wooseob Yoon](https://github.com/hyunyunV)
*   [Beomsu Lee](https://github.com/dldks321)
*   [Jongmok Lee](https://github.com/lijm1358)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
