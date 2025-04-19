import Alert from "@mui/material/Alert";
import MainTitle from "../../components/MainTitle";
import { useNavigate } from "react-router-dom";
import { Button } from "@mui/material";

/**
 * ErrorPage component displays a 404 error message when the page is not found.
 * @returns {JSX.Element} The rendered ErrorPage component.
 */

const ErrorPage = () => {
  const navigate = useNavigate();
  return (
    <div>
      <MainTitle>404. Page not found.</MainTitle>
      <Button onClick={() => navigate("/")}>Return to Main Page</Button>
      <Alert severity="error" className="mt-4">
        Oops! Something went wrong. Please try again later.
      </Alert>
    </div>
  );
};

export default ErrorPage;
