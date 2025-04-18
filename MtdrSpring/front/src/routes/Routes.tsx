/**
 * The Router component sets up application routing using react-router-dom.
 * @returns A JSX element that encapsulates the routing configuration.
 */

import { BrowserRouter, Route, Routes } from "react-router-dom";
import { Typography } from "@mui/material";
import MainPage from "../pages/Main";
import KPIPage from "../pages/Kpi";
import ErrorPage from "../pages/Error";
import TeamStats from "../pages/TeamStats";

const Router = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/kpi" element={<KPIPage />} />
        <Route path="/test" element={<Typography> Hello World </Typography>} />
        <Route path="/stats" element={<TeamStats />} />
        <Route path="*" element={<ErrorPage />} />
      </Routes>
    </BrowserRouter>
  );
};

export default Router;
