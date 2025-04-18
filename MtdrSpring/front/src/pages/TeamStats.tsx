import { useEffect, useState } from "react";
import MainTitle from "../components/MainTitle";
import { User } from "../models/User";
import { getUsers } from "../api/user";
import { Typography } from "@mui/material";

/**
 * Page made for Team Stats
 */

const TeamStats = () => {
  // TODO: Add amount of available hours to each Member to calculate total availability time.
  const [users, setUsers] = useState<User[]>([]);
  let maxHours = 0;
  useEffect(() => {
    getUsers().then((payload) => {
      if (!payload) return;
      setUsers(payload);
    });
  }, []);

  useEffect(() => {
    users.map(() => {
      // Iterate through users and always be adding +1
      maxHours += 1;
    });
  }, [users]);

  return (
    <div>
      <MainTitle>Team Statistics</MainTitle>


      {/**
       * !! Just a placeholder for now.
       */}

      <Typography>Max Hours: {maxHours}</Typography>

      {users.map((user) => (
        <div key={user.id_Telegram}>{user.name}</div>
      ))}
    </div>
  );
};

export default TeamStats;
