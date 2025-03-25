import React, { useState } from "react";
import { useForm, Controller } from "react-hook-form";
import {
  Modal,
  Box,
  TextField,
  Button,
  Typography,
  Checkbox,
  FormControlLabel,
  InputAdornment,
  IconButton,
} from "@mui/material";
import { ToDoElement } from "../../models/ToDoElement";
import { addItem } from "../../api/todo";
import CalendarMonthIcon from "@mui/icons-material/CalendarMonth";

interface AddModalProps {
  open: boolean;
  onClose: () => void;
  reloadTable: () => void;
  setLoading: React.Dispatch<React.SetStateAction<boolean>>;
}

const AddModal: React.FC<AddModalProps> = ({
  open,
  onClose,
  reloadTable,
  setLoading,
}) => {
  const [hasDeliveryDate, setDeliveryDate] = useState<boolean>(false);

  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<ToDoElement>();

  const handleFormSubmit = (data: ToDoElement) => {
    console.log(`JSON: ${JSON.stringify(data)}`);
    setLoading(true);
    addItem(data)
      .catch((error) => {
        console.error("There was an error!", error);
      })
      .then(() => {
        reloadTable();
      });
    onClose(); // Close the modal :3
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
          Add Project
        </Typography>
        <form onSubmit={handleSubmit(handleFormSubmit)}>
          <Controller
            name="description"
            control={control}
            defaultValue=""
            rules={{ required: "Description is required" }}
            render={({ field }) => (
              <TextField
                {...field}
                label="Description"
                fullWidth
                margin="normal"
                error={!!errors.description}
                helperText={
                  errors.description ? errors.description.message : ""
                }
              />
            )}
          />

          {/**
           * Delivery Date will only be asked if you check the box for it.
           */}
          <FormControlLabel
            label="Has Delivery Date?"
            control={
              <Checkbox onClick={() => setDeliveryDate(!hasDeliveryDate)} />
            }
          />
          {hasDeliveryDate && (
            <Controller
              name="delivery_ts"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Delivery Date"
                  type="datetime-local"
                  fullWidth
                  margin="normal"
                  style={{
                    color: "black",
                  }}
                  InputLabelProps={{
                    shrink: true,
                  }}
                  inputProps={{
                    step: 1, // To allow seconds to be set
                  }}
                  value={
                    field.value
                      ? new Date(field.value).toISOString().substring(0, 16)
                      : new Date().toISOString().substring(0, 16)
                  }
                  onChange={(e) => field.onChange(new Date(e.target.value))}
                  InputProps={{
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          onClick={() => {
                            const inputElement =
                              document.getElementById("delivery_ts-input");
                            if (
                              inputElement &&
                              inputElement instanceof HTMLInputElement &&
                              typeof inputElement.showPicker === "function"
                            ) {
                              inputElement.showPicker();
                            }
                          }}
                        >
                          <CalendarMonthIcon />
                        </IconButton>
                      </InputAdornment>
                    ),
                  }}
                />
              )}
            />
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
