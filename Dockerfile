FROM python:3.8
ENV PYTHONUNBUFFERED 1
WORKDIR /usr/src/app
COPY server/requirements.txt ./
RUN pip install --upgrade pip==21.2.4
RUN pip install -r requirements.txt

COPY . .
WORKDIR /usr/src/app/server
CMD bash -c "python3 manage.py makemigrations && python3 manage.py migrate && 	python3 manage.py runserver 0:8000"
EXPOSE 8000