import { Button } from "@mui/material";
import { useNavigate } from "react-router-dom";
import ArrowBackIosNewIcon from "@mui/icons-material/ArrowBackIosNew";
const ReturnButton = () => {
  const navigate = useNavigate();

  const handleReturn = () => {
    navigate(-1);
  };

  return (
    <Button className="return-button" onClick={handleReturn}>
      <ArrowBackIosNewIcon sx={{ marginRight: 1 }} />
      Return
    </Button>
  );
};
export default ReturnButton;
