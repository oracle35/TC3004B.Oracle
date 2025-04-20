import { Alert } from "@mui/material";
import { useEffect, useState } from "react";
import { Sprint } from "../models/Sprint";
import { getSprintById } from "../api/sprint";
import { isSelectedSprintExpired } from "../utils/sprint";

interface SprintWarningInterface {
  selectedSprint: number | string;
}

/**
 * Component used to indicate whether a selected sprint has already expired or it doesnt exist anymore.
 * TODO: Will refactor this entire thing into using syncronous.
 * ?? It will stop making unneccessary requests to the backend.
 */

const SprintWarning = ({ selectedSprint }: SprintWarningInterface) => {
  const [selectedSprintData, setSelectedSprintData] = useState<Sprint>();
  const [isSprintExpired, setIsSprintExpired] = useState<boolean>(false);

  useEffect(() => {
    if (typeof selectedSprint == "string") {
      console.log("All Selected");
      return;
    }

    getSprintById(selectedSprint).then((payload) => {
      if (!payload) return;
      setSelectedSprintData(payload);
    });
  }, [selectedSprint]);

  useEffect(() => {
    const fetchExpiredSprint = async () => {
      try {
        if (!selectedSprintData) return;

        const [isSprintExpiredResponse] = await Promise.all([
          isSelectedSprintExpired(selectedSprintData),
        ]);
        setIsSprintExpired(isSprintExpiredResponse);
      } catch (error) {
        console.error(error);
      }
    };
    fetchExpiredSprint();
  }, [selectedSprintData]);

  if (isSprintExpired) {
    return <Alert severity="warning">This sprint has expired.</Alert>;
  }
  return <div />;
};

export default SprintWarning;
