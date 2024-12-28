import {exec} from 'child_process';

export function umbrellaMonitorAdapter(
  server: string,
  route: string,
  req,
  res,
  next,
  responseData = null
) {
  exec([
    'curl',
    '--request POST',
    '--header "Content-Type: application/json"',
    `--data '${JSON.stringify({
      requestUrlParameters: req.params,
      requestBodyPayload: req.body,
      context: req.ip,
      server: server,
      route: route,
      responseBodyPayload: responseData
    })}'`,
    'localhost:8081'
  ].join(' '), (error, stdout, stderr) => {
    console.log('umbrella monitor output:');
    console.log(stdout);

    if (error !== null) {
      res.status(500).send({error: "umbrella monitor not available"});
      return;
    }

    const response = JSON.parse(stdout);

    if (response.status === 'invalid request') {
      res.status(500).send({error: 'invalid umbrella monitor request'});
      return;
    }

    if (response.status !== "ok") {
      res.status(403).send({error: 'verification failed'});
      return;
    }

    if (responseData !== null) {
      res.send(responseData);
      return;
    }

    next();
  });
}