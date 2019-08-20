import React from 'react';

import { Notification, NotificationGroup } from '@progress/kendo-react-notification';
import { Fade, Slide } from '@progress/kendo-react-animation';


const position = {
  topLeft: { top: 70, left: 0, alignItems: 'flex-start' },
  topCenter: { top: 70, left: '50%', transform: 'translateX(-50%)' },
  topRight: { top: 70, right: 0, alignItems: 'flex-end' },
  bottomLeft: { bottom: 0, left: 0, alignItems: 'flex-start' },
  bottomCenter: { bottom: 0, left: '50%', transform: 'translateX(-50%)' },
  bottomRight: { bottom: 0, right: 0, alignItems: 'flex-end' }
};



export const infoMessage = (msg) => {
  alert('' + msg);
};

export const warnMessage = (msg) => {
  alert('' + msg);
};

export const errorMessage = (msg) => {
  alert('' + msg);
};

export const infoNotification = (msg) => (

  <div>

  
  <Slide direction={'down'}>

    <Notification
      type={{ style: 'info', icon: true }}
      closable={true}
      style={{ overflow: 'visible' }}
      onClose={() => ({})}
    >
      <span>{msg}</span>
    </Notification>
  </Slide>
  </div>
);


export const warnNotification = (msg) => (
     <div>
    <NotificationGroup style={position.topCenter}>
      <Notification
        type={{ style: 'warning', icon: true }}
        closable={true}>
        <span>{msg}</span>
      </Notification>
    </NotificationGroup>
    </div>
);

export const errorNotification = (msg) => (
  <Fade enter={true} exit={true}>
    <NotificationGroup style={position.bottomCenter}>
      <Notification
        type={{ style: 'error', icon: true }}
        closable={true}>

        <span>{msg}</span>
      </Notification>
    </NotificationGroup>
  </Fade>
);