FROM registry.cn-hangzhou.aliyuncs.com/library/node:20-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
RUN npm run build

FROM registry.cn-hangzhou.aliyuncs.com/library/alpine:3.20
WORKDIR /app
COPY --from=builder /app/dist /opt/dist
RUN mkdir -p /frontend_dist
CMD ["sh", "-c", "rm -rf /frontend_dist/* && cp -r /opt/dist/. /frontend_dist/ && echo 'frontend assets synced' && tail -f /dev/null"]
