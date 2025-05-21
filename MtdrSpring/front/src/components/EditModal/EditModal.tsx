import React, { JSX, useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import {
  Alert,
  Autocomplete,
  Box,
  Button,
  CircularProgress,
  DialogTitle,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Modal,
  Select,
  Slider,
  TextField,
  Typography,
} from "@mui/material";
import { Task } from "../../models/Task";
import { User } from "../../models/User";
import { Sprint } from "../../models/Sprint";
import { getUsers } from "../../api/user";
import { getSprints } from "../../api/sprint";
import { updateTask } from "../../api/task"; // Ensure this API function exists
import { DatePicker } from "@mui/x-date-pickers";
import dayjs from "dayjs";

interface EditModalProps {
  open: boolean;
  onClose: () => void;
  taskToEdit: Task | undefined;
  onTaskUpdated: (updatedTask: Task) => void;
}

const formDefaultValues: Task = {
  id_Task: 0,
  description: "",
  state: "TODO",
  hoursEstimated: 0,
  hoursReal: 0,
  assignedTo: 0,
  id_Sprint: 0,
  createdAt: new Date(), // Placeholder, actual value comes from taskToEdit
  updatedAt: null,
  finishesAt: null,
};

const EditModal: React.FC<EditModalProps> = ({
  open,
  onClose,
  taskToEdit,
  onTaskUpdated,
}): JSX.Element => {
  const [showWarning, setShowWarning] = useState<boolean>(false);
  const [users, setUsers] = useState<User[]>([]);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [sprints, setSprints] = useState<Sprint[]>([]);
  const [selectedSprint, setSelectedSprint] = useState<Sprint | null>(null);

  const {
    control,
    handleSubmit,
    formState: { errors },
    setValue,
    watch,
    reset,
  } = useForm<Task>({
    defaultValues: formDefaultValues,
  });

  useEffect(() => {
    const fetchUsersAndSprints = async () => {
      try {
        const fetchedUsers = await getUsers();
        setUsers(fetchedUsers);
        const fetchedSprints = await getSprints();
        setSprints(fetchedSprints.sort((a, b) => a.name.localeCompare(b.name)));
      } catch (error) {
        console.error("Failed to fetch users or sprints:", error);
      }
    };
    fetchUsersAndSprints();
  }, []);

  useEffect(() => {
    if (open && taskToEdit) {
      reset({
        ...taskToEdit,
        hoursReal: taskToEdit.hoursReal ?? 0, // Use 0 if null for form input
      });
      if (users.length > 0) {
        const userToSelect = users.find(
          (u) => u.id_User === taskToEdit.assignedTo,
        );
        setSelectedUser(userToSelect || null);
      }
      if (sprints.length > 0) {
        const sprintToSelect = sprints.find(
          (s) => s.id_Sprint === taskToEdit.id_Sprint,
        );
        setSelectedSprint(sprintToSelect || null);
      }
    } else if (!open) {
      reset(formDefaultValues);
      setSelectedUser(null);
      setSelectedSprint(null);
      setShowWarning(false);
    }
  }, [open, taskToEdit, reset, users, sprints]);

  // Update selectedUser/Sprint if users/sprints load after taskToEdit is set
  useEffect(() => {
    if (open && taskToEdit && users.length > 0 && !selectedUser) {
      const userToSelect = users.find(
        (u) => u.id_User === taskToEdit.assignedTo,
      );
      setSelectedUser(userToSelect || null);
      setValue("assignedTo", userToSelect?.id_User || 0);
    }
  }, [open, taskToEdit, users, selectedUser, setValue]);

  useEffect(() => {
    if (open && taskToEdit && sprints.length > 0 && !selectedSprint) {
      const sprintToSelect = sprints.find(
        (s) => s.id_Sprint === taskToEdit.id_Sprint,
      );
      setSelectedSprint(sprintToSelect || null);
      setValue("id_Sprint", sprintToSelect?.id_Sprint || 0);
    }
  }, [open, taskToEdit, sprints, selectedSprint, setValue]);

  const hoursEstimatedWatch = watch("hoursEstimated");
  const taskStateWatch = watch("state");
  const hoursRealWatch = watch("hoursReal");

  useEffect(() => {
    if (hoursEstimatedWatch !== null && hoursEstimatedWatch !== undefined) {
      setShowWarning(hoursEstimatedWatch > 4);
    }
  }, [hoursEstimatedWatch]);

  const handleUserChange = (user: User | null) => {
    setSelectedUser(user);
    setValue("assignedTo", user?.id_User || 0, { shouldValidate: true });
  };

  const handleSprintChange = (sprint: Sprint | null) => {
    setSelectedSprint(sprint);
    setValue("id_Sprint", sprint?.id_Sprint || 0, { shouldValidate: true });
  };

  const handleFormSubmit = async (data: Task) => {
    if (!taskToEdit) return;

    try {
      setIsSubmitting(true);
      const taskData: Task = {
        ...taskToEdit,
        description: data.description,
        state: data.state,
        hoursEstimated: data.hoursEstimated || 0,
        assignedTo: data.assignedTo,
        id_Sprint: data.id_Sprint,
        updatedAt: new Date(),
        hoursReal: data.hoursReal,
        finishesAt: data.finishesAt,
      };

      if (data.state === "DONE") {
        taskData.hoursReal = hoursRealWatch ?? null; // Use watched value, allow null
      } else {
        taskData.hoursReal = taskToEdit.hoursReal; // Preserve original if not DONE, or could be null/0
      }

      const updatedTaskResult = await updateTask(taskToEdit.id_Task, taskData);
      onTaskUpdated(updatedTaskResult);
      onClose();
      reset();
    } catch (error) {
      console.error("Error updating task:", error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal open={open} onClose={onClose}>
      <Box
        sx={{
          position: "absolute",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          width: 1000,
          height: "90vh", // Use viewport height
          maxHeight: 800, // Max height
          bgcolor: "background.paper",
          color: "black",
          border: "2px solid #000",
          boxShadow: 24,
          p: 4,
          overflow: "auto",
        }}
      >
        <DialogTitle style={{ color: "black" }}>Edit a Task</DialogTitle>
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
              min: { value: 0, message: "Hours must be non-negative" },
              max: { value: 100, message: "Maximum 100 hours" }, // Adjusted max
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
                      max={16} // Standard max for slider interaction
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
                  </Grid>
                  <Grid item>
                    <TextField
                      {...field}
                      type="number"
                      size="small"
                      error={!!errors.hoursEstimated}
                      helperText={errors.hoursEstimated?.message}
                      onChange={(e) =>
                        field.onChange(parseFloat(e.target.value))
                      }
                      InputProps={{
                        inputProps: { min: 0, max: 100, step: 1 },
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

          {taskStateWatch === "DONE" && (
            <Controller
              name="hoursReal"
              control={control}
              rules={{
                min: { value: 0, message: "Real hours must be non-negative" },
                max: {
                  value: hoursEstimatedWatch || 100,
                  message: `Max ${hoursEstimatedWatch || 100}h`,
                },
              }}
              render={({ field }) => (
                <Box sx={{ width: "100%", mt: 2 }}>
                  <Typography gutterBottom>Real Hours</Typography>
                  <Grid container spacing={2} alignItems="center">
                    <Grid item xs>
                      <Slider
                        {...field}
                        value={field.value ?? 0}
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
                          {
                            value: Math.min(4, 16),
                            label: "4h",
                          },
                          {
                            value: Math.min(8, 16),
                            label: "8h",
                          },
                          {
                            value: Math.min(12, 16),
                            label: "12h",
                          },
                          {
                            value: 16,
                            label: `${16}h`,
                          },
                        ]}
                        valueLabelDisplay="auto"
                      />
                    </Grid>
                    <Grid item>
                      <TextField
                        {...field}
                        value={field.value ?? 0}
                        type="number"
                        size="small"
                        error={!!errors.hoursReal}
                        helperText={errors.hoursReal?.message}
                        onChange={(e) =>
                          field.onChange(parseFloat(e.target.value))
                        }
                        InputProps={{
                          inputProps: {
                            min: 0,
                            max: hoursEstimatedWatch || 100,
                            step: 1,
                          },
                        }}
                        sx={{ width: "80px" }}
                      />
                    </Grid>
                  </Grid>
                </Box>
              )}
            />
          )}

          {/* Finish Date Controller */}
          <Controller
            control={control}
            name="finishesAt"
            rules={{ required: true }}
            render={({ field }) => {
              return (
                <DatePicker
                  format="DD-MM-YYYY"
                  label="Finishes At"
                  value={dayjs(field.value)}
                  inputRef={field.ref}
                  onChange={(date) => {
                    field.onChange(date);
                  }}
                  slotProps={{ textField: { fullWidth: true } }}
                />
              );
            }}
          />

          <Controller
            name="assignedTo"
            control={control}
            rules={{
              required: "Assignee is required",
              validate: (value) => value > 0 || "Assignee is required",
            }}
            render={() => (
              <Autocomplete
                options={users}
                getOptionLabel={(option) =>
                  `${option.name} (${option.position})`
                }
                value={selectedUser}
                onChange={(_, newValue) => {
                  handleUserChange(newValue); // Updates selectedUser and form value
                }}
                isOptionEqualToValue={(option, value) =>
                  option.id_User === value?.id_User
                }
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
            )}
          />

          <Controller
            name="id_Sprint"
            control={control}
            // Add rules if sprint is mandatory
            render={() => (
              <Autocomplete
                options={sprints}
                getOptionLabel={(option) => option.name}
                value={selectedSprint}
                onChange={(_, newValue) => {
                  handleSprintChange(newValue); // Updates selectedSprint and form value
                }}
                isOptionEqualToValue={(option, value) =>
                  option.id_Sprint === value?.id_Sprint
                }
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Select Sprint"
                    fullWidth
                    margin="normal"
                    // error={!!errors.id_Sprint}
                    // helperText={errors.id_Sprint?.message}
                  />
                )}
              />
            )}
          />

          {showWarning && (
            <Alert severity="warning" sx={{ mt: 2 }}>
              This task exceeds the recommended 4-hour limit. Consider breaking
              it down if possible.
            </Alert>
          )}

          <Box
            sx={{
              display: "flex",
              justifyContent: "flex-end",
              mt: 3,
              pt: 2,
              borderTop: "1px solid lightgray",
            }}
          >
            <Button onClick={onClose} sx={{ mr: 2 }}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="contained"
              color="primary"
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <CircularProgress size={24} color="inherit" />
              ) : (
                "Save Changes"
              )}
            </Button>
          </Box>
        </form>
      </Box>
    </Modal>
  );
};

export default EditModal;
