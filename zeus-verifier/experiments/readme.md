# Running Experiments
The `experiments` directory including this `readme.md` also contains some folders. In each folder there is a Rain program and a `run.sh` file to execute the respective experiment. However, make sure to do the following first.
- Start the zeus compiler and verifier by running `python3 run.py`. Note that the runner will first build the required `.jar` files to then execute them.
- Next, it is necessary to generate some sources to let zeus API and compiler communicate. To accomplish this, from the project's root, go to `zeus-api` and run `./docker/openapi-generator.sh`
- Now, the zeus API must be started. Also from `zeus-api` run `npm i` to install dependencies followed by `npm start` to start the API.
- Now, from the folder containing it, `./run.sh` may be executed to run the related experiment

After running an experiment, to avoid incorrect behavior, compiler and verifier must be restarted before running another one. Note that the verifier will only use the minimum amount of nodes: a root, as well as a gateway and worker for each domain.
