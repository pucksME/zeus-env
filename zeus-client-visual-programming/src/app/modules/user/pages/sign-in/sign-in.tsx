import React, { useState } from 'react';

import './sign-in.module.scss';
import { Box, Button, Typography } from '@material-ui/core';
import Input, { InputType } from '../../../../components/input/input';
import LockIcon from '@material-ui/icons/Lock';
import { UserService } from '../../services/user.service';
import { useStore } from '../../../../store';

/* eslint-disable-next-line */
export interface SignInProps {}

export function SignIn(props: SignInProps) {

  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [firstName, setFirstName] = useState<string>('');
  const [lastName, setLastName] = useState<string>('');
  const [confirmedPassword, setConfirmedPassword] = useState<string>('');
  const [wrongCredentials, setWrongCredentials] = useState<boolean>(false);
  const [isInSignUpMode, setIsInSignUpMode] = useState<boolean>(false);

  const signIn = useStore(state => state.signIn);

  const handleSignIn = async () => {

    if (email.length === 0 || password.length === 0) {
      return;
    }
    const jwtToken = await UserService.authenticate({email, password});
    if (jwtToken === null) {
      setPassword('');
      setWrongCredentials(true);
      return;
    }
    signIn(jwtToken.token);
    setWrongCredentials(false);
  }

  const handleSignUp = async () => {

    if (email.length === 0 || password.length === 0 || firstName.length === 0 || lastName.length === 0) {
      return;
    }

    if (password !== confirmedPassword) {
      return;
    }

    const userDto = await UserService.save({
      firstName,
      lastName,
      email,
      password
    });

    setIsInSignUpMode(false);
    setFirstName('');
    setLastName('')
    setPassword('')
  }

  const handleGoToSignUp = () => {
    if (password !== '') {
      setPassword('');
    }
    setIsInSignUpMode(true);
  }

  const buildSignInForm = () => <div>
    <div>
      <Typography variant={'body1'}>E-Mail</Typography>
      <Input style={{width: '100%'}} value={email} onChange={(event) => setEmail(event.value as string)}/>
    </div>
    <div style={{marginTop: 5}}>
      <Typography variant={'body1'}>Password</Typography>
      <Input type={InputType.PASSWORD} style={{width: '100%'}} value={password} onChange={(event) => setPassword(event.value as string)} onSubmit={handleSignIn}/>
    </div>
    <div style={{
      display: 'flex',
      justifyContent: 'space-between',
      marginTop: 5
    }}>
      <Button onClick={handleGoToSignUp}>Create Account</Button>
      <Button onClick={handleSignIn}>Sign In</Button>
    </div>
  </div>

  const buildSignUpForm = () => <div>
    <div style={{textAlign: 'center'}}>
      <Typography variant={'h5'}>Sign Up</Typography>
    </div>
    <div style={{display: 'flex', justifyContent: 'space-between', marginTop: 10}}>
      <div style={{marginRight: 15}}>
        <Typography variant={'body1'}>First Name</Typography>
        <Input value={firstName} onChange={(event) => setFirstName(event.value as string)}/>
      </div>
      <div>
        <Typography variant={'body1'}>Last Name</Typography>
        <Input value={lastName} onChange={(event) => setLastName(event.value as string)}/>
      </div>
    </div>
    <div style={{marginTop: 5}}>
      <Typography variant={'body1'}>E-Mail</Typography>
      <Input style={{width: '100%'}} value={email} onChange={(event) => setEmail(event.value as string)}/>
    </div>
    <div style={{marginTop: 5}}>
      <Typography variant={'body1'}>Password</Typography>
      <Input type={InputType.PASSWORD} style={{width: '100%'}} value={password} onChange={(event) => setPassword(event.value as string)}/>
    </div>
    <div style={{marginTop: 5}}>
      <Typography variant={'body1'}>Confirm Password</Typography>
      <Input type={InputType.PASSWORD} style={{width: '100%'}} value={confirmedPassword} onChange={(event) => setConfirmedPassword(event.value as string)}/>
    </div>
    <div style={{marginTop: 5, display: 'flex', justifyContent: 'space-between'}}>
      <Button onClick={() => setIsInSignUpMode(false)}>Cancel</Button>
      <Button onClick={handleSignUp}>Create Account</Button>
    </div>
  </div>

  return (
    <div className={'height-100-percent'} style={{
      backgroundColor: '#f7f7f7',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    }}>
      <Box style={{
        backgroundColor: '#ffffff',
        borderRadius: 15,
        boxSizing: 'border-box',
        padding: 25,
        position: 'relative',
        textAlign: 'center',
        height: 435,
        width: 350
      }}
      boxShadow={1}
      >
        <Typography variant={'h5'}>Sign In</Typography>
        <div style={{
          backgroundColor: '#eeeeee',
          borderRadius: 100,
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          height: 100,
          width: 100,
          marginTop: 10,
          position: 'relative'
        }}>
          <LockIcon style={{fontSize: 40}} color={'primary'}/>
        </div>
        <Box
          style={{
            backgroundColor: '#ffffff',
            borderRadius: 15,
            boxSizing: 'border-box',
            padding: 25,
            position: 'absolute',
            bottom: 15,
            left: 15,
            textAlign: 'left',
            width: 320
          }}
        boxShadow={1}
        >
          {!isInSignUpMode ? buildSignInForm() : buildSignUpForm()}
        </Box>

      </Box>
    </div>
  );
}

export default SignIn;
