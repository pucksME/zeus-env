import {exec} from 'child_process';

export function umbrellaMonitorAdapter(route: string, req, res, next) {
  exec([
    'java', '-jar', '../monitors/umbrella/umbrella-1.0-SNAPSHOT.jar'
  ].join(' '), (error, stdout, stderr) => {
    console.log('umbrella monitor output:');
    console.log(stdout);
  });

  next();
}