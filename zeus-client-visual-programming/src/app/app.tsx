import { Route } from 'react-router-dom';
import Navigation from './sections/navigation/navigation';
import spacing from '../assets/styling/spacing.json';
import { QueryClient, QueryClientProvider } from 'react-query';
import Project from './modules/project/pages/project/project';
import Projects from './modules/project/pages/projects/projects';
import SignIn from './modules/user/pages/sign-in/sign-in';
import { useStore } from './store';

export function App() {

  const userSession = useStore(state => state.userSession);
  const queryClient = new QueryClient();

  return (
    <QueryClientProvider client={queryClient}>
      {(userSession === null)
        ? <SignIn/>
      : <div className={'height-100-percent'}>
          <Navigation />
          <div className={'height-100-percent'}
               style={{ boxSizing: 'border-box', paddingTop: spacing.navigation.height }}>
            <Route exact path='/' render={() => <Projects />} />
            <Route exact path='/project/:projectUuid' render={() => <Project />} />
            <Route exact path='/project/:projectUuid/component/:componentUuid' render={() => <Project />} />
          </div>
        </div>}

    </QueryClientProvider>
  );
}

export default App;
