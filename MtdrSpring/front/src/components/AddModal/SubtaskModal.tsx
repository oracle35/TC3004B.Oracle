import React, { useEffect, useState } from "react";
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
import { createTask } from "../../api/task";
import { DatePicker } from "@mui/x-date-pickers";
import dayjs from "dayjs";

interface SubtaskModalProps {
  open: boolean;
  onClose: () => void;
  parentTask: Task;
  users: User[];
  onSubtaskAdded: (newSubtask: Task) => void;
  maxHours: number;
}

const SubtaskModal: React.FC<SubtaskModalProps> = ({
  open,
  onClose,
  parentTask,
  users,
  onSubtaskAdded,
  maxHours,
}) => {
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [showWarning, setShowWarning] = useState<boolean>(false);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);

  const {
    control,
    handleSubmit,
    formState: { errors },
    setValue,
    watch,
    reset,
  } = useForm<Omit<Task, "createdAt" | "updatedAt" | "id">>({
    defaultValues: {
      description: "",
      state: "TODO",
      hoursEstimated: 0,
      hoursReal: 0,
      assignedTo: 0,
      id_Sprint: parentTask.id_Sprint,
      id_Task: 0,
      finishesAt: null,
    },
  });

  useEffect(() => {
    if (!open) {
      reset({
        description: "",
        state: "TODO",
        hoursEstimated: 0,
        hoursReal: 0,
        assignedTo: 0,
        id_Sprint: parentTask.id_Sprint,
        id_Task: 0,
        finishesAt: null,
      });
      setSelectedUser(null);
    }
  }, [open, reset, parentTask.id_Sprint]);

  const hoursEstimated = watch("hoursEstimated") || 0;
  const taskState = watch("state");

  useEffect(() => {
    if (hoursEstimated !== null && hoursEstimated !== undefined) {
      setShowWarning(hoursEstimated > 4 || hoursEstimated > maxHours);
    }
  }, [hoursEstimated, maxHours]);

  const handleUserChange = (user: User | null) => {
    setSelectedUser(user);
    setValue("assignedTo", user?.id_User || 0);
  };

  const handleFormSubmit = async (
    data: Omit<Task, "createdAt" | "updatedAt" | "id">,
  ) => {
    try {
      setIsSubmitting(true);
      const hours = data.hoursEstimated || 0;
      if (hours > maxHours) {
        throw new Error(`Subtask hours cannot exceed ${maxHours} hours`);
      }
      const taskData = {
        ...data,
        hoursEstimated: hours,
        createdAt: new Date(),
        finishesAt: data.finishesAt,
      };

      const createdTask = await createTask(taskData);
      onSubtaskAdded(createdTask);
      onClose();

      // Reset the form so that the modal is fresh on next open
      reset({
        description: "",
        state: "TODO",
        hoursEstimated: 0,
        hoursReal: 0,
        assignedTo: 0,
        id_Sprint: parentTask.id_Sprint,
        id_Task: 0,
        finishesAt: undefined,
      });
    } catch (error) {
      console.error("There was an error!", error);
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
          width: 600,
          bgcolor: "background.paper",
          color: "black",
          border: "2px solid #000",
          boxShadow: 24,
          p: 4,
        }}
      >
        <DialogTitle style={{ color: "black" }}>Add a Subtask</DialogTitle>
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
              max: {
                value: maxHours,
                message: `Maximum ${maxHours} hours per subtask`,
              },
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
                        inputProps: { min: 0, max: 16, step: 1 },
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
                  <MenuItem value="QA">QA</MenuItem>
                  <MenuItem value="DONE">Done</MenuItem>
                  <MenuItem value="ON_HOLD">On Hold</MenuItem>
                  <MenuItem value="BLOCKED">Blocked</MenuItem>
                </Select>
              </FormControl>
            )}
          />

          {taskState === "DONE" && (
            <Controller
              name="hoursReal"
              control={control}
              rules={{
                min: { value: 0, message: "Real hours must be positive" },
              }}
              render={({ field }) => (
                <Box sx={{ width: "100%", mt: 2 }}>
                  <Typography gutterBottom>Real Hours</Typography>
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
                        max={4}
                        step={1}
                        marks={[
                          {
                            value: Math.min(0, 4),
                            label: "0h",
                          },
                          {
                            value: Math.min(1, 4),
                            label: "1h",
                          },
                          {
                            value: Math.min(2, 4),
                            label: "2h",
                          },
                          {
                            value: Math.min(3, 4),
                            label: "3h",
                          },
                          {
                            value: Math.min(4, 4),
                            label: "4h",
                          },
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
                        error={!!errors.hoursReal}
                        helperText={errors.hoursReal?.message}
                        onChange={(e) => {
                          const value = parseFloat(e.target.value);
                          field.onChange(value);
                        }}
                        InputProps={{
                          inputProps: {
                            min: 0,
                            max: hoursEstimated || 4,
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

          {/* Warning message for exceeding max hours (4). */}

          {showWarning && (
            <Alert severity="warning" sx={{ mt: 2 }}>
              <Typography>
                This subtask exceeds the recommended 4-hour limit. Please
                consider breaking it down further.
              </Typography>
            </Alert>
          )}
          <Box sx={{ display: "flex", justifyContent: "flex-end", mt: 2 }}>
            <Button onClick={onClose} sx={{ mr: 2 }}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="contained"
              color="primary"
              disabled={showWarning || isSubmitting}
            >
              {isSubmitting ? (
                <CircularProgress size={24} color="inherit" />
              ) : (
                "Add Subtask"
              )}
            </Button>
          </Box>
        </form>
      </Box>
    </Modal>
  );
};

export default SubtaskModal;
