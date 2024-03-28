import { ConflictException, HttpException, Logger } from '@nestjs/common';

// https://www.postgresql.org/docs/current/errcodes-appendix.html (accessed 05/05/2021, 16:28)
export enum errorCodeTypes {
  UNIQUE_VIOLATION = '23505'
}

export abstract class DatabaseUtils {

  static mapErrorCodeToHttpException(errorCode: string, errorCodesMessages: { [key in errorCodeTypes]: string } = null): HttpException | null {

    if (!errorCode) {
      Logger.warn(`Can't get http exception for invalid error code "${errorCode}"`);
    }

    const message = (errorCodesMessages[errorCode]) ? errorCodesMessages[errorCode] : '';

    switch (errorCode) {
      case errorCodeTypes.UNIQUE_VIOLATION:
        return new ConflictException(message);
      default:
        Logger.warn(`Error code "${errorCode}" is not handled for getting the related http exception`);
        return null;
    }

  }

}
