import React, { useState, useEffect } from "react";
import { useForm, Controller } from "react-hook-form";
import {
  Modal,
  Box,
  TextField,
  Button,
  Typography,
  Alert,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Autocomplete,
  Slider,
  Grid,
  List,
  ListItem,
  ListItemText,
  IconButton,
  CircularProgress,
} from "@mui/material";
import { Task } from "../../models/Task";
import { User } from "../../models/User";
import { createTask } from "../../api/task";
import { getUsers } from "../../api/user";
import SubtaskModal from "./SubtaskModal";
import AddIcon from "@mui/icons-material/Add";
import DeleteIcon from "@mui/icons-material/Delete";
import { createTaskDependency } from "../../api/taskDependency";

interface AddModalProps {
  open: boolean;
  onClose: () => void;
  reloadTable: () => void;
  setLoading: React.Dispatch<React.SetStateAction<boolean>>;
  sprintId: number;
  addTask: (task: Task) => void;
}

const AddModal: React.FC<AddModalProps> = ({
  open,
  onClose,
  sprintId,
  addTask,
}): JSX.Element => {
  const [showWarning, setShowWarning] = useState<boolean>(false);
  const [users, setUsers] = useState<User[]>([]);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [showSubtaskModal, setShowSubtaskModal] = useState<boolean>(false);
  const [currentTask, setCurrentTask] = useState<Task | null>(null);
  const [subtasks, setSubtasks] = useState<Task[]>([]);
  const [remainingHours, setRemainingHours] = useState<number>(0);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);

  useEffect(() => {
    const fetchUsers = async () => {
      const fetchedUsers = await getUsers();
      setUsers(fetchedUsers);
    };
    fetchUsers();
  }, []);

  const {
    control,
    handleSubmit,
    formState: { errors },
    setValue,
    watch,
    reset,
  } = useForm<Omit<Task, "createdAt" | "updatedAt" | "finishesAt" | "id">>({
    defaultValues: {
      description: "",
      state: "TODO",
      hoursEstimated: 0,
      hoursReal: 0,
      assignedTo: 0,
      id_Sprint: sprintId,
    },
  });

  const hoursEstimated = watch("hoursEstimated");

  useEffect(() => {
    if (hoursEstimated !== null && hoursEstimated !== undefined) {
      setShowWarning(hoursEstimated > 4);
      setRemainingHours(hoursEstimated);
    }
  }, [hoursEstimated]);

  const handleUserChange = (user: User | null) => {
    setSelectedUser(user);
    setValue("assignedTo", user?.id_User || 0);
  };

  const handleAddSubtask = () => {
    setShowSubtaskModal(true);
  };

  const handleSubtaskAdded = async (newSubtask: Task) => {
    const subtaskHours = newSubtask.hoursEstimated || 0;
    // Update the form field so that any UI (slider/number field) reflects the new value.
    const currentHours = watch("hoursEstimated") || 0;
    const newRemaining = Math.max(0, currentHours - subtaskHours);
    setValue("hoursEstimated", newRemaining);
    setSubtasks((prev) => [...prev, newSubtask]);
    setShowSubtaskModal(false);
  };

  const handleFormSubmit = async (
    data: Omit<Task, "createdAt" | "updatedAt" | "finishesAt" | "id">
  ) => {
    try {
      setIsSubmitting(true);
      const taskData = {
        ...data,
        hoursEstimated: remainingHours,
        createdAt: new Date(),
      };
      const createdTask = await createTask(taskData);
      setCurrentTask(createdTask);
      addTask(createdTask);
      // Create dependencies after the main task is created
      for (const subtask of subtasks) {
        await createTaskDependency({
          id_ParentTask: createdTask.id_Task,
          id_ChildTask: subtask.id_Task,
        });
      }
      // Reset the form and clear local state so that the modal is fresh next time
      reset({
        description: "",
        state: "TODO",
        hoursEstimated: 0,
        hoursReal: 0,
        assignedTo: 0,
        id_Sprint: sprintId,
      });
      setSubtasks([]);
      setRemainingHours(0);
      setSelectedUser(null);
      setCurrentTask(null);
    } catch (error) {
      console.error("There was an error!", error);
    } finally {
      setIsSubmitting(false);
      onClose();
    }
  };

  return (
    <>
      <Modal open={open} onClose={onClose}>
        <Box
          sx={{
            position: "absolute",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            width: 1000,
            height: 800,
            bgcolor: "background.paper",
            color: "black",
            border: "2px solid #000",
            boxShadow: 24,
            p: 4,
            overflow: "auto",
          }}
        >
          <Typography variant="h6" component="h2">
            Add Task
          </Typography>
          <form onSubmit={handleSubmit(handleFormSubmit)}>
            <Controller
              name="description"
              control={control}
              rules={{ required: "Description is required" }}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Description"
                  fullWidth
                  margin="normal"
                  error={!!errors.description}
                  helperText={errors.description?.message}
                />
              )}
            />
            <Controller
              name="hoursEstimated"
              control={control}
              rules={{
                required: "Estimated hours are required",
                min: { value: 0, message: "Hours must be greater than 0" },
                max: { value: 16, message: "Maximum 16 hours per task" },
              }}
              render={({ field }) => (
                <Box sx={{ width: "100%", mt: 2 }}>
                  <Typography gutterBottom>Estimated Hours</Typography>
                  <Grid container spacing={2} alignItems="center">
                    <Grid item xs>
                      <Slider
                        {...field}
                        value={field.value || 0}
                        onChange={(_, value) => {
                          const numValue =
                            typeof value === "number" ? value : value[0];
                          field.onChange(numValue);
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
                        valueLabelFormat={(value) => `${value}h`}
                      />
                    </Grid>
                    <Grid item>
                      <TextField
                        {...field}
                        type="number"
                        size="small"
                        error={!!errors.hoursEstimated}
                        helperText={errors.hoursEstimated?.message}
                        onChange={(e) => {
                          const value = parseFloat(e.target.value);
                          field.onChange(value);
                        }}
                        InputProps={{
                          inputProps: { min: 0, max: 16, step: 0.5 },
                        }}
                        sx={{ width: "80px" }}
                      />
                    </Grid>
                  </Grid>
                </Box>
              )}
            />
            <Controller
              name="state"
              control={control}
              render={({ field }) => (
                <FormControl fullWidth margin="normal">
                  <InputLabel>State</InputLabel>
                  <Select {...field} label="State">
                    <MenuItem value="TODO">To Do</MenuItem>
                    <MenuItem value="IN_PROGRESS">In Progress</MenuItem>
                    <MenuItem value="DONE">Done</MenuItem>
                  </Select>
                </FormControl>
              )}
            />
            <Autocomplete
              options={users}
              getOptionLabel={(option) => `${option.name} (${option.position})`}
              value={selectedUser}
              onChange={(_, newValue) => handleUserChange(newValue)}
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="Assign to"
                  fullWidth
                  margin="normal"
                  required
                  error={!!errors.assignedTo}
                  helperText={errors.assignedTo?.message}
                />
              )}
            />
            {showWarning && (
              <Alert severity="warning" sx={{ mt: 2 }}>
                This task exceeds the recommended 4-hour limit. Consider
                breaking it down into smaller subtasks.
                {remainingHours > 0 && (
                  <Typography variant="body2" sx={{ mt: 1 }}>
                    Remaining hours to distribute: {remainingHours}h
                  </Typography>
                )}
              </Alert>
            )}
            <Box sx={{ mt: 2 }}>
              <Typography variant="subtitle1">Subtasks</Typography>
              <Button
                startIcon={<AddIcon />}
                onClick={handleAddSubtask}
                disabled={!showWarning}
              >
                Add Subtask
              </Button>
              <List>
                {subtasks.map((subtask) => (
                  <ListItem
                    key={subtask.id_Task}
                    secondaryAction={
                      <IconButton edge="end" aria-label="delete">
                        <DeleteIcon />
                      </IconButton>
                    }
                  >
                    <ListItemText
                      primary={subtask.description}
                      secondary={`${subtask.hoursEstimated}h - ${subtask.state}`}
                    />
                  </ListItem>
                ))}
              </List>
            </Box>
            <Box sx={{ display: "flex", justifyContent: "flex-end", mt: 2 }}>
              <Button onClick={onClose} sx={{ mr: 2 }}>
                Cancel
              </Button>
              <Button
                type="submit"
                variant="contained"
                color="primary"
                disabled={showWarning && remainingHours > 0 || remainingHours <= 0}
              >
                {isSubmitting ? (
                  <CircularProgress size={24} color="inherit" />
                ) : (
                  "Submit"
                )}
              </Button>
            </Box>
          </form>
        </Box>
      </Modal>

      {showSubtaskModal && (
        <SubtaskModal
          open={showSubtaskModal}
          onClose={() => setShowSubtaskModal(false)}
          parentTask={
            currentTask || {
              id_Task: 0,
              description: "",
              state: "TODO",
              hoursEstimated: hoursEstimated,
              hoursReal: 0,
              assignedTo: 0,
              id_Sprint: sprintId,
              createdAt: new Date(),
              updatedAt: null,
              finishesAt: null,
            }
          }
          users={users}
          onSubtaskAdded={handleSubtaskAdded}
          maxHours={remainingHours}
        />
      )}
    </>
  );
};

export default AddModal;