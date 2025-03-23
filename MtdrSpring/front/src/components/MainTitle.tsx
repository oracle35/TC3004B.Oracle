import { Typography } from "@mui/material";

const MainTitle = ({ title }: { title: string }) => {
  return (
    <Typography
      variant="h3"
      component="h3"
      gutterBottom
      sx={{
        textAlign: "left",
        margin: "20px",
        color: "#fff",
        fontFamily: "Arial, sans-serif",
        size: "1.5rem",
      }}
    >
      {title}
    </Typography>
  );
};

export default MainTitle;
