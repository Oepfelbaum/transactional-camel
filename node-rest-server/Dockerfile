FROM node:18-alpine

# Install dependencies
WORKDIR /usr/src/app
COPY package*.json ./
RUN npm install

# Copy App
COPY . .

EXPOSE 3000

CMD [ "node", "index.js"]