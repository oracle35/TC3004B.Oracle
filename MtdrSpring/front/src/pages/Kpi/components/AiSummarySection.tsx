
import React from "react";
import {
  Paper,
  Typography,
  Divider,
  CircularProgress,
  Alert,
} from "@mui/material";
import AutoAwesomeIcon from "@mui/icons-material/AutoAwesome";

interface AiSummarySectionProps {
  aiSummary: string;
  aiLoading: boolean;
  aiError: string | null;
}

const AiSummarySection: React.FC<AiSummarySectionProps> = ({
  aiSummary,
  aiLoading,
  aiError,
}) => {
  return (
    <Paper
      elevation={2}
      sx={{
        p: 2.5,
        borderColor: "primary.main",
        borderWidth: 1,
        borderStyle: "solid",
      }}
    >
      <Typography
        variant="h6"
        gutterBottom
        sx={{ display: "flex", alignItems: "center", fontWeight: "medium" }}
      >
        <AutoAwesomeIcon sx={{ mr: 1, color: "primary.main" }} />{" "}
        AI-Generated Summary
      </Typography>
      <Divider sx={{ my: 1.5 }} />
      {aiLoading && (
        <CircularProgress
          size={24}
          sx={{ display: "block", margin: "auto" }}
        />
      )}
      {aiError && <Alert severity="error">{aiError}</Alert>}
      {!aiLoading && !aiError && aiSummary && (
        <Typography
          variant="body1"
          sx={{
            fontStyle: "italic",
            color: "text.secondary",
            textAlign: "justify",
          }}
        >
          {aiSummary}
        </Typography>
      )}
      {!aiLoading && !aiError && !aiSummary && (
        <Typography
          variant="body2"
          sx={{ fontStyle: "italic", color: "text.secondary" }}
        >
          AI summary could not be generated or is empty. Check if data exists.
        </Typography>
      )}
    </Paper>
  );
};

export default AiSummarySection;