FROM node:lts-alpine

ENV NODE_ENV=production

WORKDIR /app

COPY ["package.json", "package-lock.json" , "./"]
COPY ["src", "./src"]
COPY ["dist", "./dist"]

RUN npm install --production

EXPOSE 80

CMD [ "node", "src//server/main.js"]
