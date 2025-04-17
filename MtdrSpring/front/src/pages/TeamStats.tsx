import { useEffect, useState } from "react";
import MainTitle from "../components/MainTitle";
import { User } from "../models/User";
import { getUsers } from "../api/user";

/**
 * Page made for Team Stats
 */

const TeamStats = () => {
  const [users, setUsers] = useState<User[]>([]);
  useEffect(() => {
    getUsers().then((payload) => {
      if (!payload) return;
      setUsers(payload);
    });
  }, []);
  return (
    <div>
      <MainTitle title="Team Stats" />

      {/**
       * !! Just a placeholder for now.
       */}
      {users.map((user) => (
        <div>{user.name}</div>
      ))}
    </div>
  );
};

export default TeamStats;
