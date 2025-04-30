import { Alert } from "@mui/material";
import { useEffect, useState } from "react";
import { Sprint } from "../models/Sprint";
import { isSelectedSprintExpired } from "../utils/sprint";

interface SprintWarningInterface {
  selectedSprint?: Sprint;
}

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
    return <Alert severity="warning">This sprint has expired.</Alert>;
  }
  return <div />;
};

export default SprintWarning;
