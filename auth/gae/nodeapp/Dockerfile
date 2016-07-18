FROM node:latest

COPY . /app
RUN cd /app && npm  install --silent
EXPOSE  8080 1337
CMD ["node", "/app/app.js"]
