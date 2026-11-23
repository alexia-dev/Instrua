FROM node:22-alpine AS build
ARG VITE_INSTRUA_API_URL
ENV VITE_INSTRUA_API_URL=${VITE_INSTRUA_API_URL}
WORKDIR /app
COPY package.json ./
RUN npm install --no-audit --no-fund
COPY index.html tsconfig.json vite.config.ts ./
COPY src ./src
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
