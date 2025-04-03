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
} from "@mui/material";
import { Task } from "../../models/Task";
import { User } from "../../models/User";
import { createTask } from "../../api/task";
import { getUsers } from "../../api/user";

interface AddModalProps {
  open: boolean;
  onClose: () => void;
  reloadTable: () => void;
  setLoading: React.Dispatch<React.SetStateAction<boolean>>;
  sprintId: number;
  parentTaskId?: number;
}

const AddModal: React.FC<AddModalProps> = ({
  open,
  onClose,
  reloadTable,
  setLoading,
  sprintId,
  parentTaskId,
}) => {
  const [showWarning, setShowWarning] = useState<boolean>(false);
  const [, setEstimatedHours] = useState<number>(0);
  const [users, setUsers] = useState<User[]>([]);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);

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
  } = useForm<Omit<Task, 'createdAt' | 'updatedAt' | 'finishesAt' | 'id'>>({
    defaultValues: {
      description: "",
      state: "TODO",
      hoursEstimated: 0,
      hoursReal: 0,
      assignedTo: 0,
      id_Sprint: sprintId,
      id_Task: 0,
    },
  });

  const handleHoursChange = (value: number) => {
    setEstimatedHours(value);
    setShowWarning(value > 4);
  };

  const handleUserChange = (user: User | null) => {
    setSelectedUser(user);
    setValue('assignedTo', user?.id_User || 0);
  };

  const handleFormSubmit = async (data: Omit<Task, 'createdAt' | 'updatedAt' | 'finishesAt' | 'id'>) => {
    setLoading(true);
    try {
      const taskData = {
        ...data,
        createdAt: new Date(),
      };
      await createTask(taskData);
      reloadTable();
    } catch (error) {
      console.error("There was an error!", error);
    } finally {
      setLoading(false);
      onClose();
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
          width: 400,
          bgcolor: "background.paper",
          color: "black",
          border: "2px solid #000",
          boxShadow: 24,
          p: 4,
        }}
      >
        <Typography variant="h6" component="h2">
          {parentTaskId ? "Add Subtask" : "Add Task"}
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
              max: { value: 4, message: "Maximum 4 hours per task" }
            }}
            render={({ field }) => (
              <Box sx={{ width: '100%', mt: 2 }}>
                <Typography gutterBottom>Estimated Hours</Typography>
                <Grid container spacing={2} alignItems="center">
                  <Grid item xs>
                    <Slider
                      {...field}
                      value={field.value || 0}
                      onChange={(_, value) => {
                        const numValue = typeof value === 'number' ? value : value[0];
                        field.onChange(numValue);
                        handleHoursChange(numValue);
                      }}
                      min={0}
                      max={4}
                      step={0.5}
                      marks={[
                        { value: 0, label: '0h' },
                        { value: 1, label: '1h' },
                        { value: 2, label: '2h' },
                        { value: 3, label: '3h' },
                        { value: 4, label: '4h' },
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
                        handleHoursChange(value);
                      }}
                      InputProps={{
                        inputProps: { min: 0, max: 4, step: 0.5 }
                      }}
                      sx={{ width: '80px' }}
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

          {showWarning && !parentTaskId && (
            <Alert severity="warning" sx={{ mt: 2 }}>
              This task exceeds the recommended 4-hour limit. Consider breaking it down into smaller subtasks.
            </Alert>
          )}

          {showWarning && parentTaskId && (
            <Alert severity="info" sx={{ mt: 2 }}>
              Note: This subtask exceeds the recommended 4-hour limit.
            </Alert>
          )}

          <Box sx={{ display: "flex", justifyContent: "flex-end", mt: 2 }}>
            <Button onClick={onClose} sx={{ mr: 2 }}>
              Cancel
            </Button>
            <Button type="submit" variant="contained" color="primary">
              Submit
            </Button>
          </Box>
        </form>
      </Box>
    </Modal>
  );
};

export default AddModal;
