import { Button } from "@mui/material";
import KeyboardArrowLeftIcon from "@mui/icons-material/KeyboardArrowLeft";
import { useNavigate } from "react-router-dom";

/**
 * #returnButton {
  float: left;
  margin-right: 15px;
}

 */
interface ReturnButtonProps {
  route: string;
}
const ReturnButton = ({ route }: ReturnButtonProps) => {
  const navigate = useNavigate();

  return (
    <Button
      style={{
        display: "flex",
        justifyContent: "flex-start",
      }}
      variant="outlined"
      onClick={() => navigate(route)}
      startIcon={<KeyboardArrowLeftIcon />}
    >
      Return
    </Button>
  );
};

export default ReturnButton;
