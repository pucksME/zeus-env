# https://openapi-generator.tech/docs/installation/#docker [accessed 23/5/2021, 21:51]
# https://openapi-generator.tech/docs/generators [accessed 23/5/2021, 21:51]
# https://docs.docker.com/engine/reference/run/#network-settings [accessed 23/5/2021, 21:51]
cd $0"/../../src"
sudo docker run --network="host" --rm \
  -v ${PWD}"/gen/thunder-api-client":"/thunder-api-client" openapitools/openapi-generator-cli generate \
  -i http://localhost:8080/api-json \
  -g typescript-axios \
  -o "/thunder-api-client"

  sudo chown -R $USER ./gen
  typescript_ignore="// @ts-nocheck"
  echo "${typescript_ignore}\n$(cat ./gen/thunder-api-client/api.ts)" > ./gen/thunder-api-client/api.ts
  echo "${typescript_ignore}\n$(cat ./gen/thunder-api-client/common.ts)" > ./gen/thunder-api-client/common.ts
