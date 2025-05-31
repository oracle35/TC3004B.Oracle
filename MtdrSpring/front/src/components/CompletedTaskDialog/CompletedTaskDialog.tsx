import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Slider,
} from "@mui/material";
import { Task } from "../../models/Task";

interface CompletedTaskDialogInterface {
  dialogState: {
    open: boolean;
    taskName: string;
    selectedTask: Task | null;
    hrsReales: number;
  };
  setDialogState: React.Dispatch<
    React.SetStateAction<{
      open: boolean;
      taskName: string;
      selectedTask: Task | null;
      hrsReales: number;
    }>
  >;
  handleDialogClose: () => void;
  handleConfirmDone: () => void;
}

// Modal shown when you mark a task as completed.

const CompletedTaskDialog = ({
  dialogState,
  setDialogState,
  handleConfirmDone,
  handleDialogClose,
}: CompletedTaskDialogInterface) => {
  const { open, taskName, selectedTask, hrsReales } = dialogState;

  return (
    <Dialog open={open} onClose={handleDialogClose}>
      <DialogTitle
        sx={{
          color: "black",
        }}
      >
        {selectedTask?.state === "DONE"
          ? "Modify Real Hours"
          : "Task Marked as Done"}
      </DialogTitle>
      <DialogContent>
        <p>
          The task &quot;{taskName}&quot; has been{" "}
          {selectedTask?.state === "DONE" ? "reopened" : "marked as DONE"}.
        </p>
        <Slider
          value={hrsReales}
          onChange={(_, value) => {
            const numValue = typeof value === "number" ? value : value[0];
            setDialogState((prev) => ({
              ...prev,
              hrsReales: numValue,
            }));
          }}
          min={0}
          max={16}
          step={1}
          marks={[
            { value: 0, label: "0h" },
            { value: 4, label: "4h" },
            { value: 8, label: "8h" },
            { value: 12, label: "12h" },
            { value: 16, label: "16h" },
          ]}
          valueLabelDisplay="auto"
        />
      </DialogContent>
      <DialogActions>
        <Button
          onClick={() => setDialogState((prev) => ({ ...prev, open: false }))}
        >
          Cancel
        </Button>
        <Button onClick={handleConfirmDone} color="primary" variant="contained">
          Confirm
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default CompletedTaskDialog;
