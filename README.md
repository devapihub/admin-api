### build and push docker
````
docker buildx build \
  --platform linux/amd64 \
  -t trivip002/admin-api:1.0.5 \
  --push .

