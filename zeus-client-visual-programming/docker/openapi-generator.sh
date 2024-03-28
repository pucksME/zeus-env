# https://openapi-generator.tech/docs/installation/#docker [accessed 23/5/2021, 21:51]
# https://openapi-generator.tech/docs/generators [accessed 23/5/2021, 21:51]
# https://docs.docker.com/engine/reference/run/#network-settings [accessed 23/5/2021, 21:51]
cd $0"/../../src"
sudo docker run --network="host" --rm \
  -v ${PWD}"/gen/api-client":"/api-client" openapitools/openapi-generator-cli generate \
  -i http://localhost:3333/api-json \
  -g typescript-axios \
  -o "/api-client" \
  --skip-validate-spec

  sudo chown -R $USER ./gen
