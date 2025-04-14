/**
 * The Router component sets up application routing using react-router-dom.
 * @returns A JSX element that encapsulates the routing configuration.
 */

import { BrowserRouter, Route, Routes } from "react-router-dom";
import KPI from "../pages/KPI";
import { Typography } from "@mui/material";
import Main from "../pages/Main";

const Router = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Main />} />
        <Route path="/kpi" element={<KPI />} />
        <Route path="/test" element={<Typography> Hello World </Typography>} />
      </Routes>
    </BrowserRouter>
  );
};

export default Router;
