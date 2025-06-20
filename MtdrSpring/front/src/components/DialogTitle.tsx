import { Typography } from "@mui/material";

interface IDialogTitle {
  children?: React.ReactNode;
  style?: React.CSSProperties;
}

const DialogTitle = ({ children, style }: IDialogTitle) => {
  return (
    <Typography
      variant="h6"
      component="h2"
      color="text.primary"
      style={{
        ...style,
      }}
    >
      {children}
    </Typography>
  );
};

export default DialogTitle;
