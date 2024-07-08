import { exec } from 'child_process';

export function bootsMonitorAdapter(route: string, req, res, next) {
  exec([
    'python3 ../monitors/boots/boots.py',
    '--route', route,
    '--request-parameters', JSON.stringify(req.params),
    '--request-payload', JSON.stringify(req.body),
  ].join(' '), (error, stdout, stderr) => {
    console.log('boots monitor output:')
    console.log(stdout);
  });

  next();
}