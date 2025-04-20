/**
 * Component shown in Main.tsx to indicate whether a selected sprint was already selected.
 */

import { Alert } from "@mui/material";
import { useEffect, useState } from "react";
import { Sprint } from "../models/Sprint";
import { getSprintById } from "../api/sprint";
import { isSelectedSprintExpired } from "../utils/sprint";

interface SprintWarningInterface {
  selectedSprint: number | string;
}


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
    return <Alert severity="warning">This is a warning Alert.</Alert>;
  }
  return <div/>;
};

export default SprintWarning;
