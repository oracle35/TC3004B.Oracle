/**
 * Message model
 * This model represents a message in the system
 * It contains information about the message such as its ID, content, date/time posted,
 * and the ID (user or entity) to whom it is addressed (notified).*/
export interface Message {
  id_message: number;
  content: string;
  postedAt: Date;
  notificateTo: number;
}
