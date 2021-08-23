FROM python:3.8
ENV PYTHONUNBUFFERED 1
WORKDIR /usr/src/app
COPY server/requirements.txt ./
RUN pip install --upgrade pip
RUN pip install -r requirements.txt

COPY . .
WORKDIR server
CMD ["python3", "manage.py", "runserver", "0:8000"]
EXPOSE 8000