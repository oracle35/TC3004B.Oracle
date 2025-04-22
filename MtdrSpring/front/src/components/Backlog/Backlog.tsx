import { Drawer } from "@mui/material";

interface BacklogDrawerProps {
  open: boolean;
  onClose: (arg0: boolean) => void;
}

const BacklogDrawer = ({ open, onClose }: BacklogDrawerProps) => {
  return (
    <Drawer open={open} onClose={onClose} anchor="right">
      This is an example of how it should look.
    </Drawer>
  );
};

export default BacklogDrawer;
