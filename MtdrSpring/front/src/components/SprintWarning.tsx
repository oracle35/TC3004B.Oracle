import { Alert } from "@mui/material";
import { useEffect, useState } from "react";
import { Sprint } from "../models/Sprint";
import { isSelectedSprintExpired } from "../utils/sprint";

interface SprintWarningInterface {
  selectedSprint?: Sprint;
}

// This component checks if the selected sprint is expired and displays a warning if it is.

const SprintWarning = ({ selectedSprint }: SprintWarningInterface) => {
  const [isSprintExpired, setIsSprintExpired] = useState<boolean>(false);

  useEffect(() => {
    const fetchExpiredSprint = async () => {
      try {
        if (!selectedSprint) return;

        const [isSprintExpiredResponse] = await Promise.all([
          isSelectedSprintExpired(selectedSprint),
        ]);
        setIsSprintExpired(isSprintExpiredResponse);
      } catch (error) {
        console.error(error);
      }
    };
    fetchExpiredSprint();
  }, [selectedSprint]);

  if (isSprintExpired) {
    return (
      <Alert
        severity="warning"
        variant="outlined"
        sx={{
          marginBottom: 2,
        }}
      >
        This sprint has expired.
      </Alert>
    );
  }
  // If the sprint is not expired, we return an empty div
  // to avoid rendering anything in the UI.
  return <div />;
};

export default SprintWarning;
