import { StrictMode } from 'react';
import * as ReactDOM from 'react-dom';
import { BrowserRouter } from 'react-router-dom';
import '@fontsource/roboto';
import App from './app/app';
import { createMuiTheme, ThemeProvider } from '@material-ui/core/styles';
import colors from './assets/styling/colors.json';
import './styles.css';

const theme = createMuiTheme({
  palette: {
    primary: {
      main: colors.primary.main,
      dark: colors.primary.dark,
      light: colors.primary.light,
      contrastText: colors.primary.contrastText
    },
    secondary: {
      main: colors.secondary.main,
      dark: colors.secondary.dark,
      light: colors.secondary.light,
      contrastText: colors.secondary.contrastText
    },
    text: {
      primary: colors.text.primary,
      secondary: colors.text.secondary
    }
  }
});

theme.shadows[1] = '0px 2px 15px rgba(0, 0, 0, 0.1)';

ReactDOM.render(
  <StrictMode>
    <BrowserRouter>
      {/* https://material-ui.com/customization/theming/ */}
      <ThemeProvider theme={theme}>
        <App />
      </ThemeProvider>
    </BrowserRouter>
  </StrictMode>,
  document.getElementById('app')
);
