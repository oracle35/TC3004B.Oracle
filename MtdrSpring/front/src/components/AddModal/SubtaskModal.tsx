import React, { useState, useEffect } from "react";
import { useForm, Controller } from "react-hook-form";
import {
  Modal,
  Box,
  TextField,
  Button,
  Typography,
  Alert,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Autocomplete,
  Grid,
  CircularProgress,
} from "@mui/material";
import { Task } from "../../models/Task";
import { User } from "../../models/User";
import { createTask } from "../../api/task";

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
  } = useForm<Omit<Task, 'createdAt' | 'updatedAt' | 'finishesAt' | 'id'>>({
    defaultValues: {
      description: "",
      state: "TODO",
      hoursEstimated: 0,
      hoursReal: 0,
      assignedTo: 0,
      id_Sprint: parentTask.id_Sprint,
      id_Task: 0,
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
      });
      setSelectedUser(null);
    }
  }, [open, reset, parentTask.id_Sprint]);

  const hoursEstimated = watch('hoursEstimated') || 0;

  useEffect(() => {
    if (hoursEstimated !== null && hoursEstimated !== undefined) {
      setShowWarning(hoursEstimated > 4 || hoursEstimated > maxHours);
    }
  }, [hoursEstimated, maxHours]);

  const handleHoursChange = (value: number) => {
    setShowWarning(value > 4 || value > maxHours);
  };

  const handleUserChange = (user: User | null) => {
    setSelectedUser(user);
    setValue('assignedTo', user?.id_User || 0);
  };

  const handleFormSubmit = async (
    data: Omit<Task, 'createdAt' | 'updatedAt' | 'finishesAt' | 'id'>
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
        <Typography variant="h6" component="h2">
          Add Subtask
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
              max: { value: maxHours, message: `Maximum ${maxHours} hours per subtask` }
            }}
            render={({ field }) => (
              <Grid container spacing={2} alignItems="center">
                <Grid item xs>
                  <TextField
                    {...field}
                    type="number"
                    label="Estimated Hours"
                    fullWidth
                    margin="normal"
                    error={!!errors.hoursEstimated}
                    helperText={errors.hoursEstimated?.message}
                    onChange={(e) => {
                      const value = parseFloat(e.target.value);
                      field.onChange(value);
                      handleHoursChange(value);
                    }}
                    InputProps={{
                      inputProps: { min: 0, max: maxHours, step: 1}
                    }}
                  />
                </Grid>
              </Grid>
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
              {hoursEstimated > maxHours 
                ? `This subtask exceeds the maximum allowed hours (${maxHours}h). Please reduce the hours.`
                : "This subtask exceeds the recommended 4-hour limit. Please break it down further."}
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
              {isSubmitting ? <CircularProgress size={24} color="inherit" /> : "Add Subtask"}
            </Button>
          </Box>
        </form>
      </Box>
    </Modal>
  );
};

export default SubtaskModal;