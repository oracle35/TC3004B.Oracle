import { Typography } from "@mui/material";
import Alert from "@mui/material/Alert";

/**
 * ErrorPage component displays a 404 error message when the page is not found.
 * @returns {JSX.Element} The rendered ErrorPage component.
 */

const ErrorPage = () => {
  return (
    <div>
     <Typography variant="h3" gutterBottom>
        Error. 404 - Page Not Found
      </Typography>
      <Alert severity="error" className="mt-4">
        Oops! Something went wrong. Please try again later.
      </Alert>
    </div>
  );
};

export default ErrorPage;
