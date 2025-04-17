/**
 * The Router component sets up application routing using react-router-dom.
 * @returns A JSX element that encapsulates the routing configuration.
 */

import { BrowserRouter, Route, Routes } from "react-router-dom";
import { Typography } from "@mui/material";
import Main from "../pages/Main";
import KPI from "../pages/KPI";
import ErrorPage from "../pages/Error";
import TeamStats from "../pages/TeamStats";

const Router = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Main />} />
        <Route path="/kpi" element={<KPI />} />
        <Route path="/test" element={<Typography> Hello World </Typography>} />
        <Route path="/stats" element={<TeamStats />} />
        <Route path="*" element={<ErrorPage />} />
      </Routes>
    </BrowserRouter>
  );
};

export default Router;
